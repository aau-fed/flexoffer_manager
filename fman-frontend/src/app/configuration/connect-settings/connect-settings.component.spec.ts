import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectSettingsComponent } from './connect-settings.component';

describe('ConnectSettingsComponent', () => {
  let component: ConnectSettingsComponent;
  let fixture: ComponentFixture<ConnectSettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConnectSettingsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
