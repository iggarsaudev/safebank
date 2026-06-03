import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { AccountResponse } from '../../models/account.models';
import { AuthService } from '../../../auth/services/auth.service';
import { TransactionService } from '../../../transactions/services/transaction.service';
import { Transaction } from '../../../transactions/models/transaction.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink], // RouterLink es vital para el botón
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly accountService = inject(AccountService);
  private readonly authService = inject(AuthService);
  private readonly transactionService = inject(TransactionService);
  private readonly router = inject(Router);

  accountData = signal<AccountResponse | null>(null);
  isLoading = signal<boolean>(true);

  transactionsData = signal<Transaction[]>([]);
  isLoadingTransactions = signal<boolean>(true);

  ngOnInit(): void {
    this.loadAccount();
    this.loadTransactions();
  }

  private loadAccount(): void {
    this.accountService.getMyAccount().subscribe({
      next: (data) => {
        this.accountData.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('error al recuperar la cuenta bancaria', error);
        this.isLoading.set(false);
        this.logout();
      },
    });
  }

  private loadTransactions(): void {
    this.transactionService.getMyTransactions().subscribe({
      next: (data) => {
        this.transactionsData.set(data);
        this.isLoadingTransactions.set(false);
      },
      error: (error) => {
        console.error('error al recuperar transacciones', error);
        this.isLoadingTransactions.set(false);
      },
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
