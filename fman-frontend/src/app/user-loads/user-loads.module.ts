import { ModuleWithProviders, NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { CommonModule } from "@angular/common";
import { TabsModule } from "ngx-bootstrap/tabs";

import { UserLoadsComponent } from "./user-loads.component";
import { AccordionModule } from "ngx-bootstrap/accordion";

import { AuthGuard, SharedModule } from "../shared";

const activeLoadRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: "user-loads",
    component: UserLoadsComponent
  }
]);

@NgModule({
  imports: [
    CommonModule,
    activeLoadRouting,
    SharedModule,
    AccordionModule.forRoot(),
    TabsModule.forRoot()
  ],
  declarations: [UserLoadsComponent],
  providers: []
})
export class UserLoadModule {}
