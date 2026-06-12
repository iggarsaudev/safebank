import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountResponse } from '../models/account.models';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/accounts`;

  // método para obtener la cuenta del usuario logueado actualmente
  getMyAccount(): Observable<AccountResponse> {
    return this.http.get<AccountResponse>(`${this.API_URL}/my-account`);
  }
}
