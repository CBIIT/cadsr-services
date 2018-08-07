import { TestBed, inject } from '@angular/core/testing';

import { FormListService } from './formlist.service';

describe('FormListService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FormListService]
    });
  });

  it('should be created', inject([FormListService], (service: FormListService) => {
    expect(service).toBeTruthy();
  }));
});
