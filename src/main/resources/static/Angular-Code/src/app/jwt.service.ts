import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class JwtService {

  private jwtToken: string | null = null;
  private profilePicture$ = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {
    this.jwtToken = localStorage.getItem('jwt');
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
    return this.http.get('http://localhost:8080/user-registration/validateJwt', {
      headers: { 'Authorization': `Bearer ${this.jwtToken}` }
    });
  }

  setToken(token: string): void {
    this.jwtToken = token;
    localStorage.setItem('jwt', token);
  }

  getToken(): string | null {
    return this.jwtToken;
  }

  removeToken(): void {
    this.jwtToken = null;
    localStorage.removeItem('jwt');
  }
}