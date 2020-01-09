import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CadsrXmlComponent } from './cadsr-xml.component';

describe('CadsrXmlComponent', () => {
  let component: CadsrXmlComponent;
  let fixture: ComponentFixture<CadsrXmlComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CadsrXmlComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CadsrXmlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
