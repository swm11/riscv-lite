	.file	"fib.c"
	.option nopic
	.text
	.align	2
	.globl	fib
	.type	fib, @function
fib:
	li	a5,1
	bgtu	a0,a5,.L8
	li	a0,1
	ret
.L8:
	addi	sp,sp,-16
	sw	ra,12(sp)
	sw	s0,8(sp)
	sw	s1,4(sp)
	mv	s0,a0
	addi	a0,a0,-1
	call	fib
	mv	s1,a0
	addi	a0,s0,-2
	call	fib
	add	a0,s1,a0
	lw	ra,12(sp)
	lw	s0,8(sp)
	lw	s1,4(sp)
	addi	sp,sp,16
	jr	ra
	.size	fib, .-fib
	.align	2
	.globl	main
	.type	main, @function
main:
	addi	sp,sp,-16
	sw	ra,12(sp)
	li	a0,10
	call	fib
	lw	ra,12(sp)
	addi	sp,sp,16
	jr	ra
	.size	main, .-main
	.ident	"GCC: (GNU) 8.2.0"
