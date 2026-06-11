import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TransactionService } from '../../services/transaction.service';
import { ToastService } from '../../../../core/services/toast.service';
import { BeneficiaryService } from '../../../beneficiaries/services/beneficiary.service';
import { Beneficiary } from '../../../beneficiaries/models/beneficiary.models';

@Component({
  selector: 'app-transfer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './transfer-form.component.html',
})
export class TransferFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly transactionService = inject(TransactionService);
  private readonly beneficiaryService = inject(BeneficiaryService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);

  // Señal para la agenda
  beneficiaries = signal<Beneficiary[]>([]);

  // Formulario reactivo
  transferForm = this.fb.group({
    targetIban: ['', [Validators.required, Validators.minLength(15)]],
    amount: ['', [Validators.required, Validators.min(0.01)]],
    concept: [''],
  });

  isSubmitting = false;

  ngOnInit(): void {
    // Cargamos los contactos al iniciar
    this.beneficiaryService.getMyBeneficiaries().subscribe({
      next: (data) => this.beneficiaries.set(data),
      error: (err) => console.error('No se pudo cargar la agenda', err),
    });
  }

  // Autocompletado del IBAN
  onBeneficiarySelect(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const selectedIban = selectElement.value;

    if (selectedIban) {
      this.transferForm.patchValue({ targetIban: selectedIban });
      this.toastService.show('IBAN autocompletado', 'success');
    }
  }

  // Envío del formulario
  onSubmit(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    this.transactionService
      .makeTransfer(this.transferForm.value as any)
      .subscribe({
        next: (res) => {
          this.toastService.show(res.message, 'success');
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          const errorMessage =
            err.error?.message || 'Error al procesar la transferencia';
          this.toastService.show(errorMessage, 'error');
          this.isSubmitting = false;
        },
      });
  }
}
