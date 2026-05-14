import { createClient } from "npm:@supabase/supabase-js@2.49.8";
import { createRemoteJWKSet, jwtVerify } from "npm:jose@5.9.6";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const supabaseUrl = Deno.env.get("PROJECT_URL") ?? "";
const serviceRoleKey = Deno.env.get("SERVICE_ROLE_KEY") ?? "";
const firebaseProjectId = Deno.env.get("FIREBASE_PROJECT_ID") ?? "";
const bucketName = Deno.env.get("STORAGE_BUCKET") ?? "pomodoro";
const jwks = createRemoteJWKSet(new URL("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com"));

type UploadRequest = {
  userId?: string;
  imageUrl?: string;
};

function jsonResponse(body: Record<string, unknown>, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}

async function verifyFirebaseToken(authHeader: string, expectedUserId: string) {
  if (!firebaseProjectId) {
    throw new Error("FIREBASE_PROJECT_ID is not configured");
  }

  const [, token] = authHeader.split(" ");
  if (!token) {
    throw new Error("Missing bearer token");
  }

  const { payload } = await jwtVerify(token, jwks, {
    issuer: `https://securetoken.google.com/${firebaseProjectId}`,
    audience: firebaseProjectId,
  });

  const tokenUserId = typeof payload.user_id === "string"
    ? payload.user_id
    : typeof payload.sub === "string"
    ? payload.sub
    : "";

  if (!tokenUserId || tokenUserId !== expectedUserId) {
    throw new Error("Token user mismatch");
  }
}

function decodeDataUrl(imageUrl: string) {
  const match = imageUrl.match(/^data:(.+?);base64,(.+)$/);
  if (!match) {
    return null;
  }

  const [, contentType, base64] = match;
  const bytes = Uint8Array.from(atob(base64), (char) => char.charCodeAt(0));
  return {
    bytes,
    contentType,
  };
}

async function fetchImageBytes(imageUrl: string) {
  const dataUrl = decodeDataUrl(imageUrl);
  if (dataUrl) {
    return dataUrl;
  }

  const response = await fetch(imageUrl);
  if (!response.ok) {
    throw new Error(`Failed to fetch source image: ${response.status}`);
  }

  const arrayBuffer = await response.arrayBuffer();
  return {
    bytes: new Uint8Array(arrayBuffer),
    contentType: response.headers.get("content-type") ?? "image/jpeg",
  };
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (request.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  if (!supabaseUrl || !serviceRoleKey) {
    return jsonResponse({ error: "Supabase env is not configured" }, 500);
  }

  const authHeader = request.headers.get("Authorization") ?? "";
  if (!authHeader.startsWith("Bearer ")) {
    return jsonResponse({ error: "Missing Authorization header" }, 401);
  }

  let payload: UploadRequest;
  try {
    payload = await request.json();
  } catch {
    return jsonResponse({ error: "Invalid JSON body" }, 400);
  }

  const userId = payload.userId?.trim();
  const imageUrl = payload.imageUrl?.trim();
  if (!userId || !imageUrl) {
    return jsonResponse({ error: "userId and imageUrl are required" }, 400);
  }

  try {
    await verifyFirebaseToken(authHeader, userId);
  } catch (error) {
    return jsonResponse({
      error: "Unauthorized",
      details: error instanceof Error ? error.message : "Token verification failed",
    }, 401);
  }

  try {
    const { bytes, contentType } = await fetchImageBytes(imageUrl);
    const objectPath = `users/${userId}/profile.jpg`;

    const supabase = createClient(supabaseUrl, serviceRoleKey, {
      auth: { persistSession: false },
    });

    const { error } = await supabase.storage
      .from(bucketName)
      .upload(objectPath, bytes, {
        contentType,
        upsert: true,
      });

    if (error) {
      throw error;
    }

    const { data } = supabase.storage.from(bucketName).getPublicUrl(objectPath);
    return jsonResponse({ publicUrl: data.publicUrl });
  } catch (error) {
    return jsonResponse({
      error: "Upload failed",
      details: error instanceof Error ? error.message : "Unknown error",
    }, 500);
  }
});
