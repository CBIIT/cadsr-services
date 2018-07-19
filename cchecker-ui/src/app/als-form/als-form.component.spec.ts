import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AlsFormComponent } from './als-form.component';

describe('AlsFormComponent', () => {
  let component: AlsFormComponent;
  let fixture: ComponentFixture<AlsFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AlsFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AlsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
