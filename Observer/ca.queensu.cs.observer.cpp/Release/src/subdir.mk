################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CC_SRCS += \
../src/CLIUtils.cc \
../src/Config.cc \
../src/Event.cc \
../src/Method.cc \
../src/Observer.cc \
../src/Serializer.cc \
../src/Socket.cc \
../src/Text.cc 

CPP_SRCS += \
../src/RealTimeLibs.cpp 

C_SRCS += \
../src/SampleTCPServer.c 

CC_DEPS += \
./src/CLIUtils.d \
./src/Config.d \
./src/Event.d \
./src/Method.d \
./src/Observer.d \
./src/Serializer.d \
./src/Socket.d \
./src/Text.d 

OBJS += \
./src/CLIUtils.o \
./src/Config.o \
./src/Event.o \
./src/Method.o \
./src/Observer.o \
./src/RealTimeLibs.o \
./src/SampleTCPServer.o \
./src/Serializer.o \
./src/Socket.o \
./src/Text.o 

CPP_DEPS += \
./src/RealTimeLibs.d 

C_DEPS += \
./src/SampleTCPServer.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.cc
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I"/home/hili/git/Observer/Observer/ca.queensu.cs.observer.cpp/include" -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

src/%.o: ../src/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I"/home/hili/git/Observer/Observer/ca.queensu.cs.observer.cpp/include" -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


