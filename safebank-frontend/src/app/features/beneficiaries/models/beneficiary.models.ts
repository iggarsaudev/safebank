export interface Beneficiary {
  id: number;
  name: string;
  iban: string;
}

export interface BeneficiaryRequest {
  name: string;
  iban: string;
}
