import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AlsFormListComponent } from './als-form-list.component';

describe('AlsFormListComponent', () => {
  let component: AlsFormListComponent;
  let fixture: ComponentFixture<AlsFormListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AlsFormListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AlsFormListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
