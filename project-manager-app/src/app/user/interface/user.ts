export interface User {
  id: number;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  imageUrl?: string;
  isEnabled: boolean;
  isNotLocked: boolean;
  createdAt?: Date;
  roleName: string;
  permissions: string;
}
