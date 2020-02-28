import { FMANApiService } from "./shared/services/fmanapi.service";
import { AuthComponent } from "./auth/auth.component";
import { ModuleWithProviders, NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { RouterModule, Routes } from "@angular/router";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { SimpleNotificationsModule } from "angular2-notifications";

import { NgIdleKeepaliveModule } from "@ng-idle/keepalive"; // this includes both the core NgIdleModule and keepalive providers
import { MomentModule } from "angular2-moment"; // optional, provides moment-style pipes for date formatting
import { HttpClientModule } from "@angular/common/http";

import { AppComponent } from "./app.component";
import { AuthModule } from "./auth/auth.module";
import { AppMaterialModule } from "./app-material.module";

import {
  ApiService,
  ArticlesService,
  AuthGuard,
  CommentsService,
  FooterComponent,
  HeaderComponent,
  NavComponent,
  JwtService,
  ProfilesService,
  SharedModule,
  TagsService,
  UserService
} from "./shared";

import { ErrorPageModule } from "./error-page/error-page.module";

import { HeadquarterService } from "./headquarter/headquarter.service";
import { HeadquarterModule } from "./headquarter/headquarter.module";

import { DashboardModule } from "./dashboard/dashboard.module";
import { TradingModule } from "./trading/trading.module";
import { TradingHistoryModule } from "./trading-history/trading-history.module";

import { CommonService } from "./shared/services/common.service";

import { TRANSLATION_PROVIDERS, TranslateService } from "./translate";
import { UserListModule } from "./userlist/userlist.module";
/*Bootstrap 4*/

// import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

/*Bootstrap 3*/

import { ModalModule } from "ngx-bootstrap/modal";

import { MapModule } from "./user-map/map.module";
import { MapService } from "./user-map/map.service";
import { UserlistService } from "./userlist/userlist.service";
import { UserLoadModule } from "./user-loads/user-loads.module";
import { UserLoadService } from "./user-loads/user-loads.service";
import { HistoricalLoadModule } from "./user-historical-load/historical-load.module";
import { HistoricalLoadService } from "./user-historical-load/historical-load.service";
import { ConfigurationModule } from "./configuration/configuration.module";
import { ConfirmationDialogComponent } from "./shared/dialogs/confirmation-dialog/confirmation-dialog.component";
import { DropdownMenuComponent } from "./shared/layout/dropdown-menu/dropdown-menu.component";
import { UserBillModule } from "./user-bill/user-bill.module";
import { UserContractModule } from "./user-contract/user-contract.module";
import { PortfolioLoadsModule } from "./portfolio-loads/portfolio-loads.module";
import { PortfolioLoadsService } from "./portfolio-loads/portfolio-loads.service";
import { TradingKpisModule } from "./trading-kpis/trading-kpis.module";

// const appRoutes: Routes = [
//   { path: 'signup', component: AuthComponent },
//   { path: 'login', component: AuthComponent },
//   { path: 'user/list', component: UserListComponent},
//   { path: '**', component: AuthComponent }
// ];

const rootRouting: ModuleWithProviders = RouterModule.forRoot([], {
  useHash: true
});

@NgModule({
  declarations: [
    AppComponent,
    FooterComponent,
    HeaderComponent,
    NavComponent,
    ConfirmationDialogComponent,
    DropdownMenuComponent
  ],
  imports: [
    BrowserModule,
    // NgbModule.forRoot(),
    AuthModule,
    rootRouting,
    SharedModule,
    HeadquarterModule,
    DashboardModule,
    TradingModule,
    TradingHistoryModule,
    ErrorPageModule,
    UserListModule,
    MapModule,
    ModalModule.forRoot(),
    UserLoadModule,
    HistoricalLoadModule,
    AppMaterialModule,
    BrowserAnimationsModule,
    UserBillModule,
    UserContractModule,
    SimpleNotificationsModule.forRoot(),
    ConfigurationModule,
    PortfolioLoadsModule,
    // RouterModule.forRoot(appRoutes),
    MomentModule,
    NgIdleKeepaliveModule.forRoot(),
    HttpClientModule,
    TradingKpisModule
  ],
  providers: [
    ApiService,
    FMANApiService,
    ArticlesService,
    AuthGuard,
    CommentsService,
    JwtService,
    ProfilesService,
    TagsService,
    UserService,
    HeadquarterService,
    CommonService,
    TRANSLATION_PROVIDERS,
    TranslateService,
    MapService,
    UserlistService,
    UserLoadService,
    HistoricalLoadService,
    PortfolioLoadsService
  ],
  bootstrap: [AppComponent],
  entryComponents: [ConfirmationDialogComponent]
})
export class AppModule {}
