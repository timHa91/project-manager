import {User} from "./user";
import {DataState} from "../enum/data-state.enum";

export interface LoginState {
  dataState: DataState;
  isLoggedIn: boolean;
  errorMessage?: string;
  message?: string
  user?: User;
}
