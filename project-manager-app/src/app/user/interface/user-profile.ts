import {User} from "./user";

export interface UserProfile {
  user: User;
  accessToken: string;
  refreshToken: string;
}
