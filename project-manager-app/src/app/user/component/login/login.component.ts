import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {UserService} from "../../service/user.service";
import {LoginState} from "../../interface/login-state";
import {catchError, map, Observable, of, startWith} from "rxjs";
import {DataState} from "../../enum/data-state.enum";
import {NgForm} from "@angular/forms";
import {Key} from "../../enum/key.enu";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  protected readonly DataState = DataState;
  loginState$: Observable<LoginState> = of({dataState: DataState.LOADED, isLoggedIn: false});

  constructor(private _router: Router, private userService: UserService) {}

  login(loginForm: NgForm) :void {
    this.loginState$ = this.userService.login$(loginForm.value.email, loginForm.value.password)
      .pipe(map(response => {
        if(response.data) {
          localStorage.setItem(Key.TOKEN, response.data.accessToken);
          localStorage.setItem(Key.REFRESH_TOKEN, response.data.refreshToken);
          this._router.navigate(['/']);

          return ({dataState: DataState.LOADED, isLoggedIn: true});
        }
        return ({dataState: DataState.LOADED, isLoggedIn: false})
      }),
        startWith({dataState: DataState.LOADING, isLoggedIn: false}),
        catchError((error) => of({
          dataState: DataState.ERROR,
          isLoggedIn: false,
          errorMessage: error || 'An unknown error has occurred'}))
        );
  }
}
