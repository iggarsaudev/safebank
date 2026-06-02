import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info';

export interface Toast {
  message: string;
  type: ToastType;
  visible: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  // usamos signals de angular 17 para manejar el estado de forma reactiva y sin rxjs
  toastState = signal<Toast>({ message: '', type: 'info', visible: false });

  show(message: string, type: ToastType = 'info') {
    this.toastState.set({ message, type, visible: true });

    // ocultamos la notificación automáticamente después de 3 segundos
    setTimeout(() => {
      this.hide();
    }, 3000);
  }

  hide() {
    this.toastState.update((state) => ({ ...state, visible: false }));
  }
}
