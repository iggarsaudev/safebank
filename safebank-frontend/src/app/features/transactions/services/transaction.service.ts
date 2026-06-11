import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  PaginatedTransactions,
  Transaction,
  TransactionRequest,
} from '../models/transaction.models';

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/v1/transactions';

  makeTransfer(request: TransactionRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.API_URL, request);
  }

  getMyTransactions(
    page: number = 0,
    size: number = 5,
  ): Observable<PaginatedTransactions> {
    return this.http.get<PaginatedTransactions>(
      `${this.API_URL}?page=${page}&size=${size}`,
    );
  }

  downloadReceipt(transactionId: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${transactionId}/receipt`, {
      responseType: 'blob', // CRUCIAL: Le dice a Angular que viene un archivo binario
    });
  }

  getMyScheduledTransfers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/scheduled`);
  }

  cancelScheduledTransfer(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(
      `${this.API_URL}/scheduled/${id}`,
    );
  }
}
