import { Routes } from '@angular/router';

export const routes: Routes = [
  // redirección por defecto al login
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // aplicamos lazy loading (carga perezosa) para optimizar el rendimiento
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/components/login/login.component').then(
        (m) => m.LoginComponent,
      ),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/components/register/register.component').then(
        (m) => m.RegisterComponent,
      ),
  },
];
