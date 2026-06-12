import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  FormControl,
} from '@angular/forms';
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

  beneficiaries = signal<Beneficiary[]>([]);

  transferForm = this.fb.group({
    targetIban: ['', [Validators.required, Validators.minLength(15)]],
    amount: ['', [Validators.required, Validators.min(0.01)]],
    concept: [''],
    frequency: ['IMMEDIATE'],
  });

  isSubmitting = false;

  // NUEVAS VARIABLES PARA EL DOBLE FACTOR (OTP)
  showOtpModal = signal<boolean>(false);
  otpControl = new FormControl('', [
    Validators.required,
    Validators.pattern('^[0-9]{6}$'),
  ]);

  ngOnInit(): void {
    this.beneficiaryService.getMyBeneficiaries().subscribe({
      next: (data) => this.beneficiaries.set(data),
      error: (err) => console.error('No se pudo cargar la agenda', err),
    });
  }

  onBeneficiarySelect(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const selectedIban = selectElement.value;

    if (selectedIban) {
      this.transferForm.patchValue({ targetIban: selectedIban });
      this.toastService.show('IBAN autocompletado', 'success');
    }
  }

  // 1. Interceptamos el envío del formulario principal
  onSubmit(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }

    const amount = Number(this.transferForm.value.amount);

    // Si es 1000€ o más, disparamos el flujo de alta seguridad
    if (amount >= 1000) {
      this.isSubmitting = true;
      this.transactionService.requestOtp().subscribe({
        next: (res) => {
          this.toastService.show(res.message, 'success'); // Avisamos que el email salió
          this.showOtpModal.set(true); // Abrimos el modal
          this.isSubmitting = false;
        },
        error: (err) => {
          this.toastService.show(
            'Error al solicitar el código de seguridad',
            'error',
          );
          this.isSubmitting = false;
        },
      });
      return; // Detenemos la ejecución aquí, esperando a que ponga el código en el Modal
    }

    // Si es menos de 1000€, ejecutamos normalmente sin código OTP
    this.executeFinalTransfer();
  }

  // 2. Método que llama el Modal cuando el usuario mete los 6 dígitos
  submitOtp(): void {
    if (this.otpControl.invalid) {
      this.otpControl.markAsTouched();
      return;
    }
    // Ejecutamos la transferencia pasando el código OTP
    this.executeFinalTransfer(this.otpControl.value!);
  }

  // 3. Cierra el modal si se arrepiente
  cancelOtp(): void {
    this.showOtpModal.set(false);
    this.otpControl.reset();
  }

  // 4. El motor real que hace la petición a Java
  private executeFinalTransfer(otpCode?: string): void {
    this.isSubmitting = true;

    // Unimos los datos del formulario con el código OTP (si existe)
    const finalRequest = {
      ...this.transferForm.value,
      otpCode: otpCode,
    };

    this.transactionService.makeTransfer(finalRequest as any).subscribe({
      next: (res) => {
        this.toastService.show('Transferencia realizada con éxito', 'success');
        this.showOtpModal.set(false);
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
