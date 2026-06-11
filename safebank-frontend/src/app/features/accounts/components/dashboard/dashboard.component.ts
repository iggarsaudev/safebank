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
  imports: [CommonModule, RouterLink],
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

  // Señales para la paginación
  currentPage = signal<number>(0);
  totalPages = signal<number>(1);
  isFirstPage = signal<boolean>(true);
  isLastPage = signal<boolean>(true);

  ngOnInit(): void {
    this.loadAccount();
    // Cargamos la primera página (la 0) al iniciar
    this.loadTransactions(0);
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

  // Ahora recibe qué página queremos cargar
  loadTransactions(pageIndex: number): void {
    this.isLoadingTransactions.set(true);
    this.transactionService.getMyTransactions(pageIndex).subscribe({
      next: (data) => {
        // Extraemos el array real de la propiedad 'content'
        this.transactionsData.set(data.content);

        // Actualizamos las señales de paginación
        this.currentPage.set(data.number);
        this.totalPages.set(data.totalPages);
        this.isFirstPage.set(data.first);
        this.isLastPage.set(data.last);

        this.isLoadingTransactions.set(false);
      },
      error: (error) => {
        console.error('error al recuperar transacciones', error);
        this.isLoadingTransactions.set(false);
      },
    });
  }

  // Función para navegar entre páginas
  changePage(newPageIndex: number): void {
    if (newPageIndex >= 0 && newPageIndex < this.totalPages()) {
      this.loadTransactions(newPageIndex);
    }
  }

  // Función para salir
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  // Función para descargar pdf
  downloadReceipt(transactionId: number): void {
    this.transactionService.downloadReceipt(transactionId).subscribe({
      next: (blob) => {
        // 1. Creamos una URL segura local en el navegador apuntando al archivo PDF
        const url = window.URL.createObjectURL(blob);

        // 2. Creamos un elemento <a> fantasma en el DOM
        const a = document.createElement('a');
        a.href = url;
        a.download = `justificante-transferencia-${transactionId}.pdf`; // Nombre del archivo

        // 3. Añadimos el elemento, lo pulsamos y lo destruimos
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);

        // 4. Liberamos la memoria del navegador
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error al descargar el justificante', error);
      },
    });
  }
}
