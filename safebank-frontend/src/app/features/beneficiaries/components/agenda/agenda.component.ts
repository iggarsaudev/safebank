import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BeneficiaryService } from '../../services/beneficiary.service';
import { Beneficiary } from '../../models/beneficiary.models';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-agenda',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './agenda.component.html',
})
export class AgendaComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly beneficiaryService = inject(BeneficiaryService);
  private readonly toastService = inject(ToastService);

  // Señales de estado
  beneficiaries = signal<Beneficiary[]>([]);
  isLoading = signal<boolean>(true);
  isSubmitting = signal<boolean>(false);

  // Formulario para añadir contacto
  contactForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    iban: ['', [Validators.required, Validators.minLength(15)]],
  });

  ngOnInit(): void {
    this.loadBeneficiaries();
  }

  loadBeneficiaries(): void {
    this.isLoading.set(true);
    this.beneficiaryService.getMyBeneficiaries().subscribe({
      next: (data) => {
        this.beneficiaries.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error cargando contactos', err);
        this.isLoading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.beneficiaryService
      .addBeneficiary(this.contactForm.value as any)
      .subscribe({
        next: (res) => {
          this.toastService.show(res.message, 'success');
          this.contactForm.reset(); // Limpiamos el formulario
          this.loadBeneficiaries(); // Recargamos la lista automáticamente
          this.isSubmitting.set(false);
        },
        error: (err) => {
          const errorMessage =
            err.error?.message || 'Error al guardar el contacto';
          this.toastService.show(errorMessage, 'error');
          this.isSubmitting.set(false);
        },
      });
  }
}
