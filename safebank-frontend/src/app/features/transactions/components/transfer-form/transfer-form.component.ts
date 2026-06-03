import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TransactionService } from '../../services/transaction.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-transfer-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './transfer-form.component.html',
})
export class TransferFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly transactionService = inject(TransactionService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);

  // definimos el formulario reactivo con sus validaciones
  transferForm = this.fb.group({
    targetIban: ['', [Validators.required, Validators.minLength(15)]],
    amount: ['', [Validators.required, Validators.min(0.01)]],
    concept: [''],
  });

  isSubmitting = false;

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
          // tras el éxito, volvemos al dashboard
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
