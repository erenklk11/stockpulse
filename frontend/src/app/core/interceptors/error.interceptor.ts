import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const ErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Unknown error';

      // Use backend error message if available, otherwise use generic message
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }

      return throwError(() => new Error(errorMessage));
    })
  );
};
