import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TickerTape } from './ticker-tape';

describe('TickerTape', () => {
  let component: TickerTape;
  let fixture: ComponentFixture<TickerTape>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TickerTape]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TickerTape);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
