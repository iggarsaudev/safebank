import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { AccountResponse } from '../../models/account.models';
import { AuthService } from '../../../auth/services/auth.service';
import { TransactionService } from '../../../transactions/services/transaction.service';
import { Transaction } from '../../../transactions/models/transaction.models';
import { ToastService } from '../../../../core/services/toast.service';
import { Chart, registerables } from 'chart.js';

// Registramos los componentes de Chart.js para que Angular los pueda usar
Chart.register(...registerables);

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
  private readonly toastService = inject(ToastService);

  accountData = signal<AccountResponse | null>(null);
  isLoading = signal<boolean>(true);

  transactionsData = signal<Transaction[]>([]);
  isLoadingTransactions = signal<boolean>(true);

  // SEÑAL PARA PAGOS PROGRAMADOS
  scheduledTransfers = signal<any[]>([]);

  // VARIABLES PARA ESTADÍSTICAS Y GRÁFICOS
  statisticsData = signal<{ totalIncome: number; totalExpense: number } | null>(
    null,
  );
  chart: any = null;

  currentPage = signal<number>(0);
  totalPages = signal<number>(1);
  isFirstPage = signal<boolean>(true);
  isLastPage = signal<boolean>(true);
  transferToDelete = signal<number | null>(null);

  ngOnInit(): void {
    this.loadAccount();
    this.loadTransactions(0);
    this.loadScheduledTransfers();
    this.loadStatistics();
  }

  private loadAccount(): void {
    this.accountService.getMyAccount().subscribe({
      next: (data) => {
        this.accountData.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.logout();
      },
    });
  }

  loadTransactions(pageIndex: number): void {
    this.isLoadingTransactions.set(true);
    this.transactionService.getMyTransactions(pageIndex).subscribe({
      next: (data) => {
        this.transactionsData.set(data.content);
        this.currentPage.set(data.number);
        this.totalPages.set(data.totalPages);
        this.isFirstPage.set(data.first);
        this.isLastPage.set(data.last);
        this.isLoadingTransactions.set(false);
      },
      error: () => this.isLoadingTransactions.set(false),
    });
  }

  // Carga las transferencias automáticas
  loadScheduledTransfers(): void {
    this.transactionService.getMyScheduledTransfers().subscribe({
      next: (data) => this.scheduledTransfers.set(data),
      error: (err) => console.error('Error cargando programadas', err),
    });
  }

  // Carga los sumatorios desde el backend y pinta el gráfico
  loadStatistics(): void {
    this.transactionService.getMyStatistics().subscribe({
      next: (data) => {
        this.statisticsData.set(data);
        // Damos un pequeño respiro de 100ms a Angular para que pinte el <canvas> en el HTML antes de buscarlo
        setTimeout(() => this.renderChart(data), 100);
      },
      error: (err) => console.error('Error cargando estadísticas', err),
    });
  }

  // Lógica pura de Chart.js
  renderChart(data: { totalIncome: number; totalExpense: number }): void {
    const canvas = document.getElementById('statsChart') as HTMLCanvasElement;
    if (!canvas) return;

    // Si ya existía un gráfico previo (al recargar componente), lo destruimos para no superponerlos
    if (this.chart) {
      this.chart.destroy();
    }

    this.chart = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: ['Ingresos', 'Gastos'],
        datasets: [
          {
            data: [data.totalIncome, data.totalExpense],
            backgroundColor: ['#10B981', '#EF4444'], // Verde Esmeralda y Rojo
            hoverBackgroundColor: ['#059669', '#DC2626'],
            borderWidth: 0,
            hoverOffset: 4,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '75%', // Hace que el anillo sea más fino
        plugins: {
          legend: { position: 'bottom' },
        },
      },
    });
  }

  // Abre el modal de confirmación de borrado
  confirmCancel(id: number): void {
    this.transferToDelete.set(id);
  }

  // Cierra el modal sin hacer nada
  abortCancel(): void {
    this.transferToDelete.set(null);
  }

  // Ejecuta el borrado real llamando al backend
  executeCancel(id: number): void {
    this.transactionService.cancelScheduledTransfer(id).subscribe({
      next: (res) => {
        this.toastService.show(
          res.message || 'Pago cancelado correctamente',
          'success',
        );
        this.transferToDelete.set(null);
        this.loadScheduledTransfers();
      },
      error: (err) => {
        const errorMessage = err.error?.message || 'Error al cancelar el pago';
        this.toastService.show(errorMessage, 'error');
        this.transferToDelete.set(null);
      },
    });
  }

  changePage(newPageIndex: number): void {
    if (newPageIndex >= 0 && newPageIndex < this.totalPages()) {
      this.loadTransactions(newPageIndex);
    }
  }

  downloadReceipt(transactionId: number): void {
    this.transactionService.downloadReceipt(transactionId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `justificante-transferencia-${transactionId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error al descargar el justificante', error);
      },
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
