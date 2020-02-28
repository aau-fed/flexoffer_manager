import { Component, OnInit, Input, SimpleChanges, OnChanges, ElementRef, ViewChild, HostListener, ViewContainerRef } from '@angular/core';
import * as d3 from 'd3';
import { FlexofferModalService, FOModalViewFO } from './flexoffermodal.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { MouseEvent } from '@agm/core';

@Component({
selector: 'flexoffersetview',
templateUrl: 'flexoffersetview.component.html',
styles: [`
    agm-map {
    height: 450px;
    }
`]
})
export class FlexofferSetviewComponent implements OnInit, OnChanges {

    @Input() public flexoffers: any[] = [];

    @Input() public showViewMode: boolean = true;
    @Input() public viewMode: number = 0; /* View as Graph */
    @Input() public syncTA: boolean = true; /* Sync time axis */
    @Input() public syncEA: boolean = true; /* Sync energy axis */

    public timeRange = [];
    public energyRange = [];

    zoom: number = 4;

    private dateCmp = function(a, b) {
        return (new Date(a).getTime()) - (new Date(b).getTime());
    };

    public dateDiff = function(date1, date2) {
        const d1 = new Date(date1);
        const d2 = new Date(date2);
        return (d1.getTime() - d2.getTime() ) / (1000 * 60);
    };

    public toDate = function(date) {
        return new Date(date);
    };

    constructor(private foModalService: FlexofferModalService,
                private modalService: BsModalService ) { }

    ngOnInit() {
        this.recomputeRanges();
    }

    ngOnChanges(changes: SimpleChanges) {
        this.recomputeRanges();
    }

    recomputeRanges() {

        if (this.syncTA) {
            const orderedEST = this.flexoffers.map(f => f.startAfterTime)
                                              .sort(this.dateCmp);

            const orderedLET = this.flexoffers.map(f => f.endBeforeTime)
                                              .sort(this.dateCmp);

            this.timeRange = [
                new Date(orderedEST[0]),
                new Date(orderedLET[orderedLET.length - 1])];
        } else {
            this.timeRange = null;
        }

        if (this.syncEA) {
            this.energyRange = [
                Math.min
                    .apply(
                        1e6,
                        this.flexoffers.map(f => Math.min.apply(1e6, f.flexOfferProfileConstraints
                                                         .map(s => s.energyConstraintList[0].lower))
                            )),
                Math.max
                    .apply(-1e6,
                        this.flexoffers.map(f => Math.max.apply(-1e6, f.flexOfferProfileConstraints
                                                         .map(s => s.energyConstraintList[0].upper))
                            ))];
        } else {
            this.energyRange = null;
        }

    };

    showFO(f) {
        if (f != null) {
         this.foModalService.showFoVisualization(f);
        }
    }

    mapClicked($event: MouseEvent) {

    }

    onMouseOver(infoWindow, gm) {

        if (gm.lastOpen != null) {
        gm.lastOpen.close();
        }

        gm.lastOpen = infoWindow;

        infoWindow.open();
    }

    hasFoLocation(f) {
        return f.locationId != null &&
               f.locationId.userLocation != null &&
               f.locationId.userLocation.longitude != null &&
               f.locationId.userLocation.latitude != null;
      }

      get flexOffersWithLocations() {
        return this.flexoffers.filter(f => this.hasFoLocation(f));
      }


}
