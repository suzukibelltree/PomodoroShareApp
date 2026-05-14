以下を必ずチェックする：

【Single Source of Truth】
- 状態の唯一の管理場所が明確か

【状態の種類】
- UI State / Domain State が混在していないか

【Flow設計】
- StateFlowとSharedFlowの使い分けが適切か
- collectのスコープが適切か（lifecycle考慮）

【アンチパターン】
- ViewModelでMutableStateを直接公開していないか
- UIでビジネスロジックを持っていないか

必要なら理想的な状態モデルを再設計する