import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withFetch } from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // habilitamos el cliente http usando la api fetch nativa del navegador para mayor rendimiento
    provideHttpClient(withFetch()),
  ],
};
