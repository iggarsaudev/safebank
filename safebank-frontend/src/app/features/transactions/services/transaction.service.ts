import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction, TransactionRequest } from '../models/transaction.models';

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/v1/transactions';

  makeTransfer(request: TransactionRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.API_URL, request);
  }

  getMyTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(this.API_URL);
  }
}
