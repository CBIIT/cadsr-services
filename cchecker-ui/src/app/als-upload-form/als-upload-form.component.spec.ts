import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AlsUploadFormComponent } from './als-upload-form.component';

describe('AlsUploadFormComponent', () => {
  let component: AlsUploadFormComponent;
  let fixture: ComponentFixture<AlsUploadFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AlsUploadFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AlsUploadFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
