int fib(const unsigned int a)
{
  if(a<2)
    return 1;
  else
    return fib(a-1) + fib(a-2);
}

void main(void)
{
  fib(10);
}
