import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HistoricalLoadComponent } from './historical-load.component';

describe('HistoricalLoadComponent', () => {
  let component: HistoricalLoadComponent;
  let fixture: ComponentFixture<HistoricalLoadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HistoricalLoadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HistoricalLoadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
