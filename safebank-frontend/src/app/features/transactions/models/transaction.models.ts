export interface TransactionRequest {
  targetIban: string;
  amount: number;
  concept?: string;
}

export interface Transaction {
  id: number;
  concept: string;
  amount: number;
  transactionDate: string;
  isIncoming: boolean;
  otherIban: string;
}
