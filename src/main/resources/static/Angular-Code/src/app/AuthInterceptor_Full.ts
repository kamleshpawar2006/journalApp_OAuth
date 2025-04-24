import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpErrorResponse,
  HttpClient
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, from } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import { Router } from '@angular/router';
import { JwtService } from './jwt.service';

@Injectable({
  providedIn: "root"
})
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(private jwtService: JwtService, private router: Router, private http: HttpClient) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const accessToken = localStorage.getItem('jwt');
    const refreshToken = localStorage.getItem('refresh');
    let authReq = req;

    if (accessToken && refreshToken) {
      authReq = this.addAuthHeaders(req, accessToken, refreshToken);
    }

    return next.handle(authReq).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse && error.status === 401 && !req.url.includes('/refresh-token')) {
          return this.handle401Error(req, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addAuthHeaders(request: HttpRequest<any>, accessToken: string, refreshToken: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`,
        'X-Refresh-Token': refreshToken
      }
    });
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = localStorage.getItem('refresh');
      if (refreshToken) {
        return this.refreshAccessToken(refreshToken).pipe(
          switchMap((tokens: any) => {
            this.isRefreshing = false;
            localStorage.setItem('jwt', tokens.accessToken);
            localStorage.setItem('refresh', tokens.refreshToken);
            this.refreshTokenSubject.next(tokens.accessToken);
            return next.handle(this.addAuthHeaders(request, tokens.accessToken, tokens.refreshToken));
          }),
          catchError(err => {
            this.isRefreshing = false;
            return throwError(() => err);
          })
        );
      }
    }

    return this.refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap(jwt => {
        const newRefresh = localStorage.getItem('refresh');
        return next.handle(this.addAuthHeaders(request, jwt!, newRefresh!));
      })
    );
  }

  // private refreshAccessToken(refreshToken: string): Observable<any> {
  //   return from(
  //     fetch('http://localhost:8080/user-registration/refresh-token', {
  //       method: 'POST',
  //       headers: {
  //         Authorization: `Bearer ${refreshToken}`,
  //         'Content-Type': 'application/json'
  //       }
  //     }).then(res => {
  //       if (!res.ok) {
  //         if (res.status === 401) {
  //           this.router.navigate(['/login']);
  //         }
  //         throw new Error('Failed to refresh token');
  //       }
  //       return res.json();
  //     })
  //   );
  // }

  private refreshAccessToken(refreshToken: string): Observable<any> {
    const headers = {
      Authorization: `Bearer ${refreshToken}`,
      'Content-Type': 'application/json'
    };
  
    return this.http.post('http://localhost:8080/user-registration/refresh-token', {}, { headers })
      .pipe(
        catchError(err => {
          if (err.status === 401) {
            this.jwtService.removeToken();
            this.jwtService.setRefreshTokenExpired(true);
            this.router.navigate(['/login']);
            return throwError(() => err );
          }
          return throwError(() => err.error || { error: 'Token refresh failed' });
        })
      );
  }
}