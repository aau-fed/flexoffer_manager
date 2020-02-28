import { ModuleWithProviders, NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { CommonModule } from "@angular/common";
import { TabsModule } from "ngx-bootstrap/tabs";

import { PortfolioLoadsComponent } from "./portfolio-loads.component";
import { AccordionModule } from "ngx-bootstrap/accordion";

import { AuthGuard, SharedModule } from "../shared";

const activeLoadRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: "portfolio-loads",
    component: PortfolioLoadsComponent
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
  declarations: [PortfolioLoadsComponent],
  providers: []
})
export class PortfolioLoadsModule {}
