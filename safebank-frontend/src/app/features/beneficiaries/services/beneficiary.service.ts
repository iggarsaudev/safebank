import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Beneficiary, BeneficiaryRequest } from '../models/beneficiary.models';

@Injectable({
  providedIn: 'root',
})
export class BeneficiaryService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/v1/beneficiaries';

  getMyBeneficiaries(): Observable<Beneficiary[]> {
    return this.http.get<Beneficiary[]>(this.API_URL);
  }

  addBeneficiary(request: BeneficiaryRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.API_URL, request);
  }
}
