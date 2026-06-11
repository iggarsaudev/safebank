export interface TransactionRequest {
  targetIban: string;
  amount: number;
  concept?: string;
  frequency?: 'IMMEDIATE' | 'MONTHLY';
}

export interface Transaction {
  id: number;
  concept: string;
  amount: number;
  transactionDate: string;
  isIncoming: boolean;
  otherIban: string;
}

export interface PaginatedTransactions {
  content: Transaction[]; // Aquí viene la lista real de transacciones
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  number: number; // Página actual (empieza en 0)
}
