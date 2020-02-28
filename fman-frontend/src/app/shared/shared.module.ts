import { FlexofferviewComponent } from "./helpers/flexofferview.component";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { HttpModule } from "@angular/http";
import { RouterModule } from "@angular/router";

import { AgmCoreModule } from "@agm/core";
import { AgmJsMarkerClustererModule } from "@agm/js-marker-clusterer";

import {
  ArticleListComponent,
  ArticleMetaComponent,
  ArticlePreviewComponent
} from "./helpers";
import { FavoriteButtonComponent, FollowButtonComponent } from "./buttons";
import { ListErrorsComponent } from "./list-errors.component";
import { ShowAuthedDirective } from "./show-authed.directive";

import { TranslatePipe } from "../translate/translate.pipe";

import { ModalboxComponent } from "./modalbox.component";
import { MenuComponent } from "./menu.component";
import { MdesignTableComponent } from "./mdesigntable.component";
import { FlexofferSetviewComponent } from "./helpers/flexoffersetview.component";
import { FlexofferCostviewComponent } from "./helpers/flexoffercostview.component";
import {
  FlexofferModalService,
  FOModalViewFO
} from "./helpers/flexoffermodal.service";
import { PortfolioSummaryComponent } from "./helpers/portfoliosummary.component";

import { ModalModule } from "ngx-bootstrap/modal";
import { MomentModule } from "ngx-moment";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    RouterModule,
    ModalModule,
    MomentModule,
    AgmCoreModule.forRoot({
      apiKey: "AIzaSyBN6F_ix26wGrpAtCYRxPmPbN6G5WoFpvA"
    }),
    AgmJsMarkerClustererModule
  ],
  declarations: [
    ArticleListComponent,
    ArticleMetaComponent,
    ArticlePreviewComponent,
    FavoriteButtonComponent,
    FollowButtonComponent,
    ListErrorsComponent,
    ShowAuthedDirective,
    TranslatePipe,
    ModalboxComponent,
    MenuComponent,
    MdesignTableComponent,
    FlexofferviewComponent,
    FlexofferCostviewComponent,
    FlexofferSetviewComponent,
    FOModalViewFO,
    PortfolioSummaryComponent
  ],
  entryComponents: [FOModalViewFO],
  exports: [
    ArticleListComponent,
    ArticleMetaComponent,
    ArticlePreviewComponent,
    CommonModule,
    FavoriteButtonComponent,
    FollowButtonComponent,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    ListErrorsComponent,
    RouterModule,
    ShowAuthedDirective,
    TranslatePipe,
    ModalboxComponent,
    MdesignTableComponent,
    FlexofferviewComponent,
    FlexofferCostviewComponent,
    FlexofferSetviewComponent,
    PortfolioSummaryComponent
  ],
  providers: [FlexofferModalService]
})
export class SharedModule {}
