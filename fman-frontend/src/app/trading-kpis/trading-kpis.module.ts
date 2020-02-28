import { ModuleWithProviders, NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { CommonModule } from "@angular/common";
import { TabsModule } from "ngx-bootstrap/tabs";

import { TradingKpisComponent } from "./trading-kpis.component";
import { BsDatepickerModule } from "ngx-bootstrap/datepicker";
import { AccordionModule } from "ngx-bootstrap/accordion";
import { MomentModule } from "ngx-moment";

import { AuthGuard, SharedModule } from "../shared";

const tradingKpisRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: "trading-kpis",
    component: TradingKpisComponent
  }
]);

@NgModule({
  imports: [
    CommonModule,
    tradingKpisRouting,
    BsDatepickerModule.forRoot(),
    SharedModule,
    MomentModule,
    AccordionModule.forRoot(),
    TabsModule.forRoot()
  ],
  declarations: [TradingKpisComponent],
  providers: []
})
export class TradingKpisModule {}
