import { Injectable } from '@angular/core';
import { Http, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { ReplaySubject } from 'rxjs/ReplaySubject';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { ApiService } from './api.service';
import { JwtService } from './jwt.service';
import { User } from '../models';

@Injectable()
export class UserService {
  private currentUserSubject = new BehaviorSubject<User>(new User());
  public currentUser = this.currentUserSubject.asObservable().distinctUntilChanged();

  private isAuthenticatedSubject = new ReplaySubject<boolean>(1);
  public isAuthenticated = this.isAuthenticatedSubject.asObservable();

  constructor(
    private apiService: ApiService,
    private http: Http,
    private jwtService: JwtService
  ) {
    let usr = JSON.parse(localStorage.getItem('user'));
    if (usr != null) {
      this.currentUserSubject.next(usr);
      // Set isAuthenticated to true
      this.isAuthenticatedSubject.next(true);
    }

  }

  getCurrentUserObservable(): Observable<User> {
    return this.currentUserSubject.asObservable();
  }

  setAuth(user: User, token: String) {
    // Save JWT sent from server in localstorage
    this.jwtService.saveToken(token);
    // User local storage to save all the user details
    localStorage.setItem('user', JSON.stringify(user));
    // Set current user data into observable
    this.currentUserSubject.next(user);
    // Set isAuthenticated to true
    this.isAuthenticatedSubject.next(true);
  }

  purgeAuth() {
    // alert("Purge Auth");
    // Remove JWT from localstorage
    this.jwtService.destroyToken();
    // Set current user to an empty object
    this.currentUserSubject.next(new User());
    // Set auth status to false
    this.isAuthenticatedSubject.next(false);

    //
    localStorage.removeItem('user');
  }

  private formatErrors(error: any) {
    return Observable.throw(error);
  }

  attemptAuth(type, credentials): Observable<User> {
    return this.apiService.post('/user/login', credentials)
      .catch(this.formatErrors)
      .map(data => {
        if (data !== null) {
          this.setAuth(data.user, data.token);
        }
        return data;
      }
      );
  }

  refreshToken(): Observable<User> {
    return this.apiService.post('/user/refreshToken', this.getCurrentUser())
      .catch(this.formatErrors)
      .map(data => {
        if (data !== null) {
          this.setAuth(data.user, data.token);
        }
        return data;
      });
  }

  attemptSignupOnBehalfOfAdmin(form_data): Observable<User> {
    return this.apiService.post('/user/register', form_data)
      .catch(this.formatErrors)
  }

  attemptSignup(form_data): Observable<User> {
    return this.apiService.post('/user/register', form_data)
      .catch(this.formatErrors)
      .map(
        data => {
          if (data !== null) {
            this.setAuth(data.user, data.token);
          }
          return data;
        }
      );
  }

  getCurrentUser(): User {
    // return this.currentUserSubject.value;
    return JSON.parse(localStorage.getItem('user'));
  }

  getUsers(): Observable<User[]> {
    return this.apiService.get('/user/')
      .catch(this.formatErrors);
  }


  getMaps(): Observable<any[]> {
    return this.apiService.get('/user/')
      .catch(this.formatErrors);
  }

  getUser(userName: string): Observable<User> {
    return this.apiService.get('/user/' + encodeURI(userName))
      .catch(this.formatErrors);
  }

  isLoggedIn() {
    const token = this.jwtService.getToken();
    if (token && !this.jwtService.isTokenExpired(token)) {
      return true;
    } else {
      return false;
    }
  }

  // Update the user on the server (email, pass, etc)
  update(user: User): Observable<User> {
    return this.apiService
      .put('/user/' + encodeURI(user.userName), user)
      .map(newUser => {
        // Update the currentUser observable
        if (this.getCurrentUser() && this.getCurrentUser().userName === newUser.userName) {
          this.currentUserSubject.next(newUser);
        }
        return newUser;
      });
  }

  deleteUserAccount(userName: string): any {
    return this.apiService
      .deleteRaw('/user/' + encodeURI(userName))
      .map(response => {

        if (this.getCurrentUser() && this.getCurrentUser().userName === userName) {
          this.purgeAuth();
        }

        console.log(response);
      });
  }

}
