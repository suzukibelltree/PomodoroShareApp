# upload-profile

Supabase Edge Function for this app's profile image upload flow.

## Expected request

- Method: `POST`
- Header: `Authorization: Bearer <Firebase ID token>`
- Body:

```json
{
  "userId": "firebase-uid",
  "imageUrl": "https://... or data:image/jpeg;base64,..."
}
```

## Response

```json
{
  "publicUrl": "https://<project-ref>.supabase.co/storage/v1/object/public/pomodoro/users/<uid>/profile.jpg"
}
```

## Required secrets

Set these secrets before deploying:

```bash
supabase secrets set \
  PROJECT_URL=https://pjukeyzyhhrlhabfrnvx.supabase.co \
  SERVICE_ROLE_KEY=<service-role-key> \
  FIREBASE_PROJECT_ID=<firebase-project-id> \
  STORAGE_BUCKET=pomodoro
```

## Deploy

```bash
supabase functions deploy upload-profile
```

## Notes

- The function verifies the Firebase ID token and ensures it belongs to `userId`.
- The image source can be either a normal URL or a base64 data URL.
- Uploaded files are stored at `pomodoro/users/<uid>/profile.jpg`.
