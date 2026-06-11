import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
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
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/accounts/components/dashboard/dashboard.component').then(
        (m) => m.DashboardComponent,
      ),
  },
  {
    path: 'transfer',
    loadComponent: () =>
      import('./features/transactions/components/transfer-form/transfer-form.component').then(
        (m) => m.TransferFormComponent,
      ),
  },
  {
    path: 'agenda',
    loadComponent: () =>
      import('./features/beneficiaries/components/agenda/agenda.component').then(
        (m) => m.AgendaComponent,
      ),
  },
];
