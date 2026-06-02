import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  // creamos el formulario reactivo con todos los campos requeridos por el backend
  registerForm = this.fb.nonNullable.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    // enviamos la petición de registro a spring boot
    this.authService.register(this.registerForm.getRawValue()).subscribe({
      next: (response) => {
        console.log('registro exitoso:', response.message);
        // guardamos el token y navegamos (temporalmente lo llevaremos al login)
        this.authService.saveToken(response.token);
        this.toastService.show(
          '¡cuenta creada con éxito! ahora inicia sesión.',
          'success',
        );
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('error en el registro', error);
        this.toastService.show(
          'error al crear la cuenta. verifica los datos o si el correo ya existe.',
          'error',
        );
      },
    });
  }
}
