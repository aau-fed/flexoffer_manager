export class User {
  userName: string;
  userId: string;
  firstName: string;
  lastName: string;
  password: string;
  email: string;
  role: string;
  enabled: string;
  contract: any;
  clients: any[];
  location: {
    latitude: number,
    longitude: number
  };
};
