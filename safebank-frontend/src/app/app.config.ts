import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
} from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // Esto es crucial: le dice a Angular que use nuestro interceptor para inyectar el Token
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
  ],
};
