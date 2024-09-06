import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {catchError, Observable, tap, throwError} from "rxjs";
import {CustomHttpResponse} from "../interface/custom-http-response";
import {UserProfile} from "../interface/user-profile";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly server: string = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  login$ = (email:string, password: string) :Observable<CustomHttpResponse<UserProfile>> => {
    return this.http.post(
      `${this.server}/user/login`,
      {email, password}
    ).pipe(
      tap(console.log),
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse)  {
    let errorMessage: string;
    if(error.error instanceof ErrorEvent) {
      errorMessage = `A client error occurred - ${error.error.message}`
    } else {
      if(error.error.reason) {
        errorMessage = error.error.reason;
      } else {
        errorMessage = `An error occurred - Error status ${error.status}`;
      }
    }

    return throwError(() => errorMessage);
  }
}
