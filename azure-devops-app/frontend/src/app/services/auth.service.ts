import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface AuthResponse {
  accessToken: string;
  tokenType?: string; // Usually 'Bearer'
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth'; // Adjust if your backend API is hosted elsewhere or needs full URL
  private authTokenKey = 'authToken';

  private loggedIn = new BehaviorSubject<boolean>(this.hasToken());

  constructor(private http: HttpClient) { }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.authTokenKey);
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signin`, credentials)
      .pipe(
        tap(response => {
          if (response && response.accessToken) {
            localStorage.setItem(this.authTokenKey, response.accessToken);
            this.loggedIn.next(true);
          }
        })
      );
  }

  register(userInfo: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/signup`, userInfo);
  }

  logout(): void {
    localStorage.removeItem(this.authTokenKey);
    this.loggedIn.next(false);
    // Here you might also want to navigate the user to the login page
    // import { Router } from '@angular/router';
    // this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.authTokenKey);
  }

  isLoggedIn(): Observable<boolean> {
    return this.loggedIn.asObservable();
  }

  // Optional: Add a method to get HttpHeaders with Authorization if needed frequently
  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    if (token) {
      return new HttpHeaders({
        'Authorization': `Bearer ${token}`
      });
    }
    return new HttpHeaders();
  }
}
