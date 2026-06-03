import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../features/auth/services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // inyectamos nuestro servicio de autenticación para obtener el token guardado
  const authService = inject(AuthService);
  const token = authService.getToken();

  // si tenemos un token, clonamos la petición original y le añadimos la cabecera de autorización
  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    // enviamos la petición modificada al backend
    return next(clonedRequest);
  }

  // si no hay token (ej. al hacer login o registro), enviamos la petición tal cual
  return next(req);
};
