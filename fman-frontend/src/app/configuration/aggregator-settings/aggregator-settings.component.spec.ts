import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AggregatorSettingsComponent } from './aggregator-settings.component';

describe('AggregatorSettingsComponent', () => {
  let component: AggregatorSettingsComponent;
  let fixture: ComponentFixture<AggregatorSettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AggregatorSettingsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AggregatorSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
