import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class JwtService {

  private jwtToken: string | null = null;
  private refreshToken: string | null = null;
  private refreshTokenExpired$ = new BehaviorSubject<boolean | null>(null);
  private profilePicture$ = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {
    this.jwtToken = localStorage.getItem('jwt');
    this.refreshToken = localStorage.getItem('refresh');
  }

  setRefreshTokenExpired(expired: boolean): void {
    this.refreshTokenExpired$.next(expired);
  }

  getRefreshTokenExpired() {
    return this.refreshTokenExpired$.asObservable();
  }

  setProfilePicture(profilePicture: string): void {
    this.profilePicture$.next(profilePicture);
  }

  getProfilePictureObservable() {
    return this.profilePicture$.asObservable();
  }

  getProfilePicture(): string | null {
    return this.profilePicture$.value;
  }

  validateToken() {
    return this.http.get('http://localhost:8080/user-registration/validateJwt');
  }

  setToken(token: string, refresh: any): void {
    this.jwtToken = token;
    localStorage.setItem('jwt', token);
    this.refreshToken = refresh;
    localStorage.setItem('refresh', refresh);
  }

  getToken(): string | null {
    return this.jwtToken;
  }

  getRefreshToken(): string | null {
    return this.refreshToken;
  }

  removeToken(): void {
    this.jwtToken = null;
    localStorage.removeItem('jwt');
    this.refreshToken = null;
    localStorage.removeItem('refresh');
  }
}