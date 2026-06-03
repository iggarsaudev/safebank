import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountResponse } from '../models/account.models';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/v1/accounts';

  // método para obtener la cuenta del usuario logueado actualmente
  getMyAccount(): Observable<AccountResponse> {
    return this.http.get<AccountResponse>(`${this.API_URL}/my-account`);
  }
}
