import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AlsReportComponent } from './als-report.component';

describe('AlsReportComponent', () => {
  let component: AlsReportComponent;
  let fixture: ComponentFixture<AlsReportComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AlsReportComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AlsReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
